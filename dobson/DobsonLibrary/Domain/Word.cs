using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Word
    {

        public string words { get; set; }

        public List<string> contextType { get; set; }

        public Word()
        {
            words = "";
            contextType = new List<string>();
        }

    }
}
