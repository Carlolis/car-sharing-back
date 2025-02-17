CREATE MIGRATION m15o6pg4kmiy2ylkc73cf4uyusugzfw2lmigqmyrfidxqmbfe4l5nq
    ONTO m1zlh2wskymoitnqj6j2gwsvucdqqyahoyk6nfi6oyytvzwbdoaoxa
{
  CREATE TYPE default::MessageEdge {
      CREATE REQUIRED PROPERTY answer: std::str;
      CREATE REQUIRED PROPERTY question: std::str;
  };
  CREATE TYPE default::ChatEdge {
      CREATE MULTI LINK messages: default::MessageEdge {
          CREATE CONSTRAINT std::exclusive;
      };
      CREATE REQUIRED PROPERTY date: cal::local_datetime;
      CREATE REQUIRED PROPERTY title: std::str;
  };
  CREATE TYPE default::WriterEdge {
      CREATE MULTI LINK chats: default::ChatEdge {
          CREATE CONSTRAINT std::exclusive;
      };
      CREATE REQUIRED PROPERTY name: std::str;
  };
};
