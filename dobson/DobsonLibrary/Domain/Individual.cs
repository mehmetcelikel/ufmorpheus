using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Individual
    {

        public int individualID { get; set; }
        public int phraseId { get; set; }
        public int classid { get; set; }

        public Individual()
        {
            phraseId = -1;
            individualID = -1;
            classid = -1;
        }

    }
}
