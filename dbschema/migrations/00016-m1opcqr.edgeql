CREATE MIGRATION m126ges5yk6ktdgwjxdxseblxhuxpqag4bbjzmradfzgvrdtbveirq
    ONTO m1nyds36dmgzjp6m4gdlz4r4eosz5lrhdbhasghbfemsjseumolxma
{
ALTER TYPE default::InvoiceGel {
    CREATE REQUIRED LINK gelPerson -> default::PersonGel {
        SET DEFAULT := (SELECT default::PersonGel FILTER .name = 'maé' LIMIT 1);
    };
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
