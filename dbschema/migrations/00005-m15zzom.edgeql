CREATE MIGRATION m15zzomf6yghf5bhfkales475tf7nhvakudjrsgfm3nxaen7gc45za
    ONTO m1x4hoelmlcjmrlxocme7pqv3uiqlirugjg2eq6mxm57msxmhxbgoa
{
  ALTER TYPE default::ChatSessionEdge RENAME TO default::ChatSessionGel;
  ALTER TYPE default::MessageEdge RENAME TO default::MessageGel;
  ALTER TYPE default::PersonEdge {
      DROP LINK drive_in;
  };
  ALTER TYPE default::PersonEdge RENAME TO default::PersonGel;
  ALTER TYPE default::TripEdge RENAME TO default::TripGel;
  ALTER TYPE default::TripGel {
      ALTER LINK edgeDrivers {
          RENAME TO gelDrivers;
      };
  };
  ALTER TYPE default::PersonGel {
      CREATE MULTI LINK drive_in := (.<gelDrivers[IS default::TripGel]);
  };
  ALTER TYPE default::WriterEdge RENAME TO default::WriterGel;
};
