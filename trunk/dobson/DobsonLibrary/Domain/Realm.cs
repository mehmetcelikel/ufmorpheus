using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace DobsonLibrary.Domain
{
    public class Realm
    {

        public String realm { get; set; }
        public int realmid { get; set; }

        public Realm()
        {
            realm = "";
            realmid = -1;
        }

    }
}
