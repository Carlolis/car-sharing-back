CREATE MIGRATION m1single_driver_change_multi_to_single
    ONTO m1reimbursement_field_add_to_invoice_gel
{
  ALTER TYPE default::InvoiceGel {
      DROP LINK gelPersons;
      CREATE REQUIRED LINK gelPerson: default::PersonGel;
  };
};