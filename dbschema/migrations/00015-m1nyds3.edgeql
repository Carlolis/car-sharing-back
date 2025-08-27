CREATE MIGRATION m1nyds36dmgzjp6m4gdlz4r4eosz5lrhdbhasghbfemsjseumolxma
    ONTO m1dhhe75z5noskkmajojz5unyzocev4iryopbb2nsrv5lmytlowrkq
{
  ALTER TYPE default::InvoiceGel {
      CREATE PROPERTY fileName: std::str;
  };
};
