module default {
  type TripEdge {
    required name: str;
    required date: cal::local_date;
    required distance: int16;
    required multi edgeDrivers: PersonEdge;
  }

  type PersonEdge {
    required name: str;
    multi drive_in := .<edgeDrivers[is TripEdge];
  }

  type WriterEdge {
    required name: str;
    multi chats : ChatEdge{
             constraint exclusive;
    }
  }

  type ChatEdge {
      required title: str;
      required date: cal::local_datetime;
      multi messages: MessageEdge{
               constraint exclusive;
      }
  }

  type MessageEdge {
        required question: str;
        required answer: str;
  }

}
using extension ai;