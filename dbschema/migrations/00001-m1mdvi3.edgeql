CREATE MIGRATION m1mdvi3sivkj34xaqpuuzycbjsj5vsqh32cqbf6ul3ob3ccgdksgea
    ONTO initial
{
  CREATE TYPE default::Person {
      CREATE REQUIRED PROPERTY name: std::str;
  };
  CREATE TYPE default::Travel {
      CREATE REQUIRED MULTI LINK driver: default::Person;
      CREATE REQUIRED PROPERTY date: cal::local_date;
      CREATE REQUIRED PROPERTY distance: std::int16;
      CREATE REQUIRED PROPERTY name: std::str;
  };
};
