CREATE MIGRATION m1niyi3gnvgsgjc7klp6iukyedxa7i623dgdprhwntm2y44kqw2u4q
    ONTO m1z5vmwt3knezmcr55a5nmq3sz66xv2rj467qc46gau4phrh6gtlqa
{
  ALTER TYPE default::InvoiceGel {
      ALTER LINK gelDrivers {
          RENAME TO gelPersons;
      };
  };
  ALTER TYPE default::PersonGel {
      CREATE MULTI LINK invoice_in := (.<gelPersons[IS default::InvoiceGel]);
  };
};
