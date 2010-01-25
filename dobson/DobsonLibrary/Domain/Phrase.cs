using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Phrase
    {
        public string phraseString { get; set; }
        public int phraseID { get; set; }
        public int contextID { get; set; }
        public int individualID { get; set; }
        public int classID { get; set; }

        public Phrase()
        {
            phraseString = "";
            phraseID = -1;
            contextID = -1;
            individualID = -1;
        }
        public Phrase(string phrase, int metaId, int classId)
        {
            phraseString = phrase;
            contextID = metaId;
            phraseID = -1;
            individualID = -1;
            classID = classId;
        }
    }
}
