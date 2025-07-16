CREATE MIGRATION m155en27gc7xgg7gs7ipbbfhyv3cro7gitutmogky37urktqmmybzq
    ONTO m1pm567a5ugsgzv2chj22p4tht2mmtupjtr4kpahmc45tx7jfj24xa
{
  CREATE TYPE default::InvoiceGel {
      CREATE REQUIRED MULTI LINK gelDrivers: default::PersonGel;
      CREATE REQUIRED PROPERTY amount: std::int16;
      CREATE REQUIRED PROPERTY date: std::cal::local_date;
      CREATE REQUIRED PROPERTY name: std::str;
  };
};
