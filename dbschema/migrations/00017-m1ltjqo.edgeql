CREATE MIGRATION m1ltjqodhno3vxntckm5t2y5x2y5okfhip5kv6eijbpeldllgxnfxa
    ONTO m126ges5yk6ktdgwjxdxseblxhuxpqag4bbjzmradfzgvrdtbveirq
{
  ALTER TYPE default::InvoiceGel {
      ALTER LINK gelPerson {
          RESET default;
          RESET OPTIONALITY;
      };
      CREATE LINK toDriver: default::PersonGel;
  };
};
