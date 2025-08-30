CREATE MIGRATION m1opcqrlri467guir4yi5s2ildat7jn573jixhwk5vijd73cd3oema
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
