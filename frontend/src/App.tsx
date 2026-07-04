import { Routes, Route } from "react-router-dom";
import { AppLayout } from "./layout/AppLayout";
import { RequireRole } from "./auth/RequireRole";
import { DashboardPage } from "./pages/DashboardPage";
import { CustomersPage } from "./pages/customers/CustomersPage";
import { TicketsPage } from "./pages/tickets/TicketsPage";
import { TicketDetailPage } from "./pages/tickets/TicketDetailPage";
import { OrdersPage } from "./pages/orders/OrdersPage";
import { TariffsPage } from "./pages/tariffs/TariffsPage";
import { SubscriptionsPage } from "./pages/subscriptions/SubscriptionsPage";
import { BillingPage } from "./pages/billing/BillingPage";

export function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route index element={<DashboardPage />} />
        <Route
          path="/customers"
          element={
            <RequireRole roles={["CSR", "ADMIN"]}>
              <CustomersPage />
            </RequireRole>
          }
        />
        <Route
          path="/tickets"
          element={
            <RequireRole roles={["CSR", "ADMIN"]}>
              <TicketsPage />
            </RequireRole>
          }
        />
        <Route
          path="/tickets/:id"
          element={
            <RequireRole roles={["CSR", "ADMIN"]}>
              <TicketDetailPage />
            </RequireRole>
          }
        />
        <Route
          path="/orders"
          element={
            <RequireRole roles={["CSR", "ADMIN"]}>
              <OrdersPage />
            </RequireRole>
          }
        />
        {/* Okuma her kimlik dogrulanmis role acik oldugu icin RequireRole yok (menu de rolsuz). */}
        <Route path="/tariffs" element={<TariffsPage />} />
        <Route
          path="/subscriptions"
          element={
            <RequireRole roles={["CSR", "ADMIN"]}>
              <SubscriptionsPage />
            </RequireRole>
          }
        />
        <Route
          path="/billing"
          element={
            <RequireRole roles={["BILLING_ADMIN", "CSR", "ADMIN"]}>
              <BillingPage />
            </RequireRole>
          }
        />
      </Route>
    </Routes>
  );
}
