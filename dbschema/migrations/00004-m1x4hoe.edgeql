CREATE MIGRATION m1x4hoelmlcjmrlxocme7pqv3uiqlirugjg2eq6mxm57msxmhxbgoa
    ONTO m15o6pg4kmiy2ylkc73cf4uyusugzfw2lmigqmyrfidxqmbfe4l5nq
{
  ALTER TYPE default::ChatEdge {
      DROP PROPERTY date;
  };
  ALTER TYPE default::ChatEdge RENAME TO default::ChatSessionEdge;
};
