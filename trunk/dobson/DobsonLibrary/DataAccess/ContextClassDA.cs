using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class ContextClassDA : BaseDA
    {
        public List<ContextClass> getContextClassList()
        {
            //List<ContextClass> results = new List<ContextClass>();
            List<ContextClass> results0 = new List<ContextClass>();

            NpgsqlDataReader reader0 = performQuery(SQLQueries.getAllContextClassesMorpheus3DB, DatabaseConnection.DBName.Morpheus3DB);
            while (reader0.Read() != false)
            {
                ContextClass cc = new ContextClass(reader0.GetInt32(0), -1, reader0.GetString(1));
                results0.Add(cc);
            }
            reader0.Close();
            /*base.DatabaseConnection = new DatabaseConnection(DatabaseConnection.DBName.sdb);
            NpgsqlDataReader reader = performQuery(SQLQueries.getAllContextClassesQuery,DatabaseConnection.DBName.sdb);
            while (reader.Read() != false)
            {
                ContextClass cc = ContextClass.parseClass(reader.GetInt32(0), reader.GetString(1));
                //only add cc if string isn't null
                if(cc.name != null)
                    results.Add(cc);
            }

            reader.Close(); */

            return results0;
        }

        internal void insertContextClass(ContextClass newclass)
        {
            performNonQuery(SQLQueries.insertContextClassQuery,
               new string[] { "classid", "contextid", "name" },
               new object[] { newclass.classId, newclass.contextclassId, newclass.name });
        }
    }
}
