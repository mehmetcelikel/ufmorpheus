using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class RealmDA : BaseDA
    {

        public long insertRealm(string realmString)
        {
            performNonQuery(SQLQueries.insertRealmQuery,
                            new string[] { "realm" },
                            new object[] { realmString });
            // Get the new searches searchId
            return (long)performScalarQuery(SQLQueries.getLastRealmIdQuery);

        }

        public List<Realm> getAllRealms()
        {
            NpgsqlDataReader allResults = performQuery(SQLQueries.getAllRealmsQuery);

            List<Realm> returnList = new List<Realm>();

            while (allResults.Read())
            {
                Realm currentRealm = new Realm();
                currentRealm.realm = allResults.GetString(allResults.GetOrdinal("realm"));
                currentRealm.realmid = allResults.GetInt32(allResults.GetOrdinal("realmid"));
                returnList.Add(currentRealm);
            }
            return returnList;

        }

    }
}
