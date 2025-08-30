CREATE MIGRATION m1iitjzbmammcdqqlxser5a6kp6phgh6lxpi4gqjqmwsuwrb3d4q3a
    ONTO m1nyds36dmgzjp6m4gdlz4r4eosz5lrhdbhasghbfemsjseumolxma
{
  ALTER TYPE default::InvoiceGel {
      CREATE REQUIRED LINK gelPerson -> default::PersonGel
         SET REQUIRED USING (SELECT default::PersonGel FILTER .name = 'maé');
  };
  ALTER TYPE default::PersonGel {
      ALTER LINK invoice_in {
          USING (.<gelPerson[IS default::InvoiceGel]);
          RESET CARDINALITY;
      };
  };
  ALTER TYPE default::InvoiceGel {
      DROP LINK gelPersons;
  };
  ALTER TYPE default::InvoiceGel {
      CREATE PROPERTY isReimbursement: std::bool {
          SET default := false;
      };
  };
};
