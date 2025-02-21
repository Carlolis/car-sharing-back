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
    multi chats : ChatSessionEdge{
             constraint exclusive;
    }
  }

  type ChatSessionEdge {
      required title: str;
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