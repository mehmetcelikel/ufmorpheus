
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class IndividualLinkDA : BaseDA
    {
        public void insertInstanceLink(IndividualLink link)
        {
            if (link.query != null)
            {
                performNonQuery(SQLQueries.insertQueryHasQuery,
                                new string[] { "queryid", "individualid", "io" },
                                new object[] { link.query.queryID, link.individualID, link.inOrOut });
            }
            else if (link.answer != null)
            {
                performNonQuery(SQLQueries.insertAnswerHasQuery,
                                new string[] { "answerid", "individualid", "io" },
                                new object[] { link.answer.answerID, link.individualID, link.inOrOut });
            } 
        }

    }
}