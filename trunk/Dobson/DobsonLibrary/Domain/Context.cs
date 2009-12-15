using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Context
    {

        public int contextID { get; set; }
        public string contextName { get; set; }
        
        public Context(int id, string name)
        {
            contextID = id;
            contextName = name;
        }
        public Context()
        {
            contextID = -1;
            contextName = "";
        }

    }
}
