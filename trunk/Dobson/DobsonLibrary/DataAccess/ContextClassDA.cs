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
            List<ContextClass> results = new List<ContextClass>();

            NpgsqlDataReader reader = performQuery(SQLQueries.getAllContextClassesQuery);

            while (reader.Read() != false)
                results.Add(new ContextClass(reader.GetInt32(0), -1, reader.GetString(1)));

            reader.Close();

            return results;
        }

        internal void insertContextClass(ContextClass newclass)
        {
            performNonQuery(SQLQueries.insertContextClassQuery,
               new string[] { "classid", "contextid", "name" },
               new object[] { newclass.classId, newclass.contextclassId, newclass.name });
        }
    }
}
