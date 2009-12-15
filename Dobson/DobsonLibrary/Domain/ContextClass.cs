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
    }
}
