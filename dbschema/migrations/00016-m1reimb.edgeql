CREATE MIGRATION m1reimbursement_field_add_to_invoice_gel
    ONTO m1nyds36dmgzjp6m4gdlz4r4eosz5lrhdbhasghbfemsjseumolxma
{
  ALTER TYPE default::InvoiceGel {
      CREATE PROPERTY isReimbursement: std::bool {
          SET default := false;
      };
  };
};