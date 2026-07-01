# CQRS (Command Query Responsibility Segregation) Tasarım Deseni Uygulaması

Bu doküman, `product-catalog-service` mikroservisinde gerçekleştirilen **CQRS** refactoring çalışmasını detaylandırmaktadır. 

---

## 1. CQRS Nedir ve Neden Uygulandı?

**CQRS (Command Query Responsibility Segregation)**, veri yazma (Command) ve veri okuma (Query) işlemlerinin sorumluluklarının ve modellerinin birbirinden ayrılmasını öngören bir tasarım desenidir. 

**Sağladığı Avantajlar:**
- **Sorumlulukların Ayrılması (Single Responsibility):** Yazma ve okuma yollarındaki iş mantıkları birbirinden tamamen izole edilir.
- **Performans ve Ölçeklenebilirlik:** Yazma ve okuma yolları farklı ölçekleme gereksinimlerine sahip olabilir. Okuma yükü yüksek olan sistemlerde okuma metotları/veritabanları bağımsız şekilde optimize edilebilir (örneğin Redis önbelleği ile).
- **Bakım Kolaylığı:** Kod tabanında büyük, monolitik servis sınıfları (örn: `TariffServiceImpl`) yerine, her bir operasyona özel, odaklanmış küçük sınıflar (Handlers) yer alır.

---

## 2. Neden `product-catalog-service` Tercih Edildi?

`product-catalog-service`, tarife ve paket bilgilerini yöneten, mimarideki tipik bir **Read-Heavy (Okuma Yoğun)** servistir:
1. **Yazma Sıklığı Düşüktür:** Yeni bir tarife yalnızca yönetici (`CATALOG_ADMIN`) tarafından oluşturulur (`POST /api/catalog/tariffs`).
2. **Okuma Sıklığı Çok Yüksektir:** Sistemdeki diğer tüm servisler (örn: `order-service` OpenFeign ile sipariş alırken) ve son kullanıcılar sürekli tarifeleri okur (`GET /api/catalog/tariffs/{code}`).
3. **Önbellek (Cache) Kritik Önemdedir:** Okuma yollarında Redis önbelleği (`@Cacheable`) kullanılırken, yeni tarife eklendiğinde bu önbellekler geçersiz kılınmaktadır (`@CacheEvict`).

Bu yapı, CQRS deseninin Command (yazma + cache geçersiz kılma) ve Query (okuma + önbellekten getirme) mantığını en temiz şekilde yansıtabileceği ideal bir adaydır.

---

## 3. Yeni Mimari ve Dizin Yapısı

Refactoring sonrasında `product-catalog-service` altındaki paket yapısı aşağıdaki gibi düzenlenmiştir:

```text
product-catalog-service/src/main/java/com/turkcell/productcatalogservice/
│
├── controller/
│   └── CatalogController.java              # Command ve Query Handler'ları doğrudan çağırır
│
├── cqrs/
│   ├── command/
│   │   ├── handler/
│   │   │   └── CreateTariffCommandHandler.java # DB'ye yazar ve Redis önbelleğini temizler
│   │   └── model/
│   │       └── CreateTariffCommand.java     # Yazma parametrelerini tutan immutable record
│   │
│   └── query/
│       ├── handler/
│       │   ├── GetTariffByCodeQueryHandler.java # Tekil okuma yapar (@Cacheable)
│       │   └── ListTariffsQueryHandler.java    # Sayfalı okuma yapar (@Cacheable)
│       └── model/
│           ├── GetTariffByCodeQuery.java    # Tekil sorgu parametresi
│           └── ListTariffsQuery.java        # Sayfalı sorgu parametresi (Pageable)
│
├── entity/
│   └── Tariff.java                          # JPA Veritabanı Varlığı
│
└── repository/
    └── TariffRepository.java                # Spring Data JPA Arayüzü
```

---

## 4. Uygulanan Kodların Detayları

### A. Command (Yazma) Yolu
Yeni bir tarife oluşturulduğunda önbelleğin güncel kalması için **tüm ilgili cache alanları temizlenir** (`@CacheEvict`).

*   **Command Modeli (`CreateTariffCommand.java`):**
    Yazma işlemi için gerekli olan immutable (salt-okunur) veri yapısıdır.
    ```java
    public record CreateTariffCommand(
        String code,
        String name,
        String type,
        BigDecimal monthlyFee,
        Integer minutesIncluded,
        Integer smsIncluded,
        Integer dataMbIncluded
    ) {}
    ```

*   **Command Handler (`CreateTariffCommandHandler.java`):**
    İş mantığını yürütür, veriyi veri tabanına kaydeder ve Redis cache temizliğini yönetir.
    ```java
    @Component
    public class CreateTariffCommandHandler {
        private final TariffRepository repository;

        public CreateTariffCommandHandler(TariffRepository repository) {
            this.repository = repository;
        }

        @Caching(evict = {
                @CacheEvict(value = "tariffByCode", allEntries = true),
                @CacheEvict(value = "tariffPage", allEntries = true)
        })
        public TariffResponse handle(CreateTariffCommand command) {
            Tariff tariff = new Tariff();
            // ... alan eşleştirmeleri ...
            tariff.setStatus("ACTIVE");
            tariff.setEffectiveFrom(LocalDate.now());
            return toResponse(repository.save(tariff));
        }
    }
    ```

---

### B. Query (Okuma) Yolu
Okuma istekleri doğrudan ilgili Query Handler'lar tarafından karşılanır ve Redis cache desteğiyle en yüksek performansta döner (`@Cacheable`).

*   **Get Tariff By Code Query & Handler:**
    Tekil tarife getirme işlemini yönetir.
    ```java
    // Query Modeli
    public record GetTariffByCodeQuery(String code) {}

    // Query Handler
    @Component
    public class GetTariffByCodeQueryHandler {
        private final TariffRepository repository;

        @Cacheable(value = "tariffByCode", key = "#query.code")
        public TariffResponse handle(GetTariffByCodeQuery query) {
            Tariff tariff = repository.findByCode(query.code())
                    .orElseThrow(() -> new ResourceNotFoundException("Tariff", query.code()));
            return toResponse(tariff);
        }
    }
    ```

*   **List Tariffs Query & Handler:**
    Sayfalanmış tarife listesini getirme işlemini yönetir.
    ```java
    // Query Modeli
    public record ListTariffsQuery(Pageable pageable) {}

    // Query Handler
    @Component
    public class ListTariffsQueryHandler {
        private final TariffRepository repository;

        @Cacheable(value = "tariffPage", key = "'p=' + #query.pageable.pageNumber + ';s=' + #query.pageable.pageSize")
        public RestPage<TariffResponse> handle(ListTariffsQuery query) {
            return new RestPage<>(repository.findAll(query.pageable()).map(ListTariffsQueryHandler::toResponse));
        }
    }
    ```

---

### C. Controller Entegrasyonu (`CatalogController.java`)
Eski yapıda bulunan ve hem yazmayı hem okumayı barındıran `TariffService` bağımlılığı tamamen kaldırılmıştır. Controller artık doğrudan ilgili **Command ve Query Handler** nesnelerine bağımlıdır.

```java
@RestController
@RequestMapping("/api/catalog/tariffs")
public class CatalogController {

    private final CreateTariffCommandHandler createTariffCommandHandler;
    private final GetTariffByCodeQueryHandler getTariffByCodeQueryHandler;
    private final ListTariffsQueryHandler listTariffsQueryHandler;

    public CatalogController(CreateTariffCommandHandler createTariffCommandHandler,
                             GetTariffByCodeQueryHandler getTariffByCodeQueryHandler,
                             ListTariffsQueryHandler listTariffsQueryHandler) {
        this.createTariffCommandHandler = createTariffCommandHandler;
        this.getTariffByCodeQueryHandler = getTariffByCodeQueryHandler;
        this.listTariffsQueryHandler = listTariffsQueryHandler;
    }

    @GetMapping("/{code}")
    public ApiResponse<TariffResponse> getByCode(@PathVariable String code) {
        return ApiResponse.ok(getTariffByCodeQueryHandler.handle(new GetTariffByCodeQuery(code)));
    }

    @GetMapping
    public ApiResponse<RestPage<TariffResponse>> list(Pageable pageable) {
        return ApiResponse.ok(listTariffsQueryHandler.handle(new ListTariffsQuery(pageable)));
    }

    @PostMapping
    @PreAuthorize("hasRole('CATALOG_ADMIN')")
    public ApiResponse<TariffResponse> create(@Valid @RequestBody CreateTariffRequest request) {
        CreateTariffCommand command = new CreateTariffCommand(
                request.code(), request.name(), request.type(), request.monthlyFee(),
                request.minutesIncluded(), request.smsIncluded(), request.dataMbIncluded()
        );
        return ApiResponse.ok(createTariffCommandHandler.handle(command), "Tarife olusturuldu");
    }
}
```

---

## 5. Eski Yapıdan Temel Farklar ve Değişiklikler

1. **Silinen Dosyalar:** Monolitik iş mantığı içeren `TariffService.java` ve `TariffServiceImpl.java` dosyaları projeden tamamen silinmiştir.
2. **Sıfır Dış Etki:** Yapılan tüm değişiklikler `product-catalog-service` sınırları içerisinde izole edilmiştir.
3. **HTTP API Uyumlu Kalmıştır:** Controller sınıflarındaki URL eşlemeleri (`@GetMapping`, `@PostMapping`) ve veri yapıları (`CreateTariffRequest`, `TariffResponse`) birebir korunduğu için, API Gateway yönlendirmelerinde veya `order-service` içindeki OpenFeign senkron çağrılarında hiçbir kesinti yaşanmamıştır.

---

## 6. Test Edilmesi

Yaptığımız bu implementasyonu test etmek için proje kök dizininde yer alan **[telco_crm_postman_collection.json](./telco_crm_postman_collection.json)** koleksiyonunu kullanabilirsiniz.
