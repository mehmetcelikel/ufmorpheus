using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Npgsql;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    class WebsiteDA : BaseDA
    {

        public IList<Website> getFilteredWebsites(string searchString)
        {
            string[] keywords = searchString.Split(' ');
            StringBuilder query = new StringBuilder(SQLQueries.getFilteredWebsitesQuery);
            List<string> paramNames = new List<string>();
            List<object> paramValues = new List<object>();

            for (int i = 0; i < keywords.Length; i++)
            {
                if (i == 0)
                {
                    query.AppendLine(" Where k.keywordString = :keyword0 ");
                }
                else
                {
                    query.Append(" OR k.keywordString = :keyword");
                    query.Append(i.ToString());
                    query.Append("\n");
                }

                paramNames.Add("keyword" + i.ToString());
                paramValues.Add(keywords[i]);

            }

            NpgsqlDataReader results = base.performQuery(query.ToString(), paramNames.ToArray(), paramValues.ToArray());

            List<Website> websites = new List<Website>();

            while (results.Read())
            {
                Website w = new Website();
                w.baseUrl = results.GetString(results.GetOrdinal("url"));
                websites.Add(w);
            }
            return websites;
        }

    }
}
