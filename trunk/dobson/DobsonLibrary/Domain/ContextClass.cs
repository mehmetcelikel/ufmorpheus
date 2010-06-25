using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    
    public class ContextClass
    {
        public int classId { get; set; }
        public int contextclassId { get; set; }
        public string name { get; set; }

        public ContextClass(int classId, int contextclassId, string name)
        {
            this.classId = classId;
            this.contextclassId = contextclassId;
            this.name = name;
        }

        internal static ContextClass parseClass(int id, string uri)
        {
            
            int i = uri.IndexOf("#");
            string c;
            //make string c = null if a # isn't found
            if (i == -1) { c = null; }
            else
            {
                    c = uri.Substring(i + 1);
            }
            
            
            return new ContextClass(id, -1, c);
        }
    }
}
