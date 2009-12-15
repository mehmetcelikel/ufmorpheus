using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Input
    {
        public long inputID { get; set; }
        public string name { get; set; }
        public int pageRefID {get;set;}
        public int contextClassID { get; set; }
        public int individualID { get; set; }
        public int highlightID { get; set; }

        public Input()
        {
            inputID = -1;
            name = "";
            pageRefID = -1;
            contextClassID = -1;
            individualID = -1;
            highlightID = -1;
        }

        public Input(int inputID, string name, int pageRefID, int contextClassID, int individualID, int highlightID)
        {
            this.inputID = inputID;
            this.pageRefID = pageRefID;            
            this.contextClassID = contextClassID;
            this.name = name;
            this.individualID = individualID;
            this.highlightID = highlightID;
        }
    }
}
