CREATE MIGRATION m1mlp6kxa4o3zmz6p3ubgr5mcpmmt5wcot5trj3n6wpdtktmrx22bq
    ONTO m15zzomf6yghf5bhfkales475tf7nhvakudjrsgfm3nxaen7gc45za
{
  ALTER TYPE default::WriterGel {
      ALTER LINK chats {
          ON TARGET DELETE ALLOW;
      };
  };
};
