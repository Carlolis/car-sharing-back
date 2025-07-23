CREATE MIGRATION m1z5vmwt3knezmcr55a5nmq3sz66xv2rj467qc46gau4phrh6gtlqa
    ONTO m155en27gc7xgg7gs7ipbbfhyv3cro7gitutmogky37urktqmmybzq
{
  ALTER TYPE default::TripGel {
      ALTER PROPERTY date {
          RENAME TO startDate;
      };
      CREATE REQUIRED PROPERTY endDate: std::cal::local_date {
          SET REQUIRED USING (<std::cal::local_date>{cal::to_local_date(2025, 7, 23)});
      };
  };
};
