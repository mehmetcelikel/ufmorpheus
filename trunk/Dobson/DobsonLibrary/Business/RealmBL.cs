using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class RealmBL
    {
        public long insertRealm(string realmString)
        {
            using (RealmDA da = new RealmDA())
            {
                return da.insertRealm(realmString);
            }
        }

        public List<Realm> getAllRealms()
        {
            using (RealmDA da = new RealmDA())
            {
                return da.getAllRealms();
            }
        }
    }
}

