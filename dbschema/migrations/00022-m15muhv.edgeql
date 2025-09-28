CREATE MIGRATION m15muhv6pxdhfro7abdehpaweftyugp35bzvied3tlt5ioiparvm7a
    ONTO m1rf6nfgck5vnuxszklln6gfjevllzpbq5o2evbw3hntwqrqckz4fa
{
  CREATE TYPE default::CarGel {
      CREATE REQUIRED PROPERTY mileage: std::int64;
      CREATE REQUIRED PROPERTY name: std::str;
  };
  ALTER TYPE default::InvoiceGel {
      ALTER PROPERTY mileage {
          SET TYPE std::int64;
      };
  };
  ALTER TYPE default::TripGel {
      ALTER PROPERTY distance {
          SET TYPE std::int64;
      };
  };
};
