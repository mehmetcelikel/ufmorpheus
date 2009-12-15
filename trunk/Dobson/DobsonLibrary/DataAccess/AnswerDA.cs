
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class AnswerDA : BaseDA
    {
        public long insertAnswer(Answer currentAnswer)
        {
            performNonQuery(SQLQueries.insertAnswerQuery,
                            new string[] { "answerstring", "queryid", "realmid"},
                            new object[] { currentAnswer.answerString, currentAnswer.queryID, currentAnswer.realmID});
            return (long)performScalarQuery(SQLQueries.getLastAnswerIdQuery);
        }

        public List<Answer> getAllAnswers()
        {
            NpgsqlDataReader allResults = performQuery(SQLQueries.getAllAnswersQuery);

            List<Answer> returnList = new List<Answer>();

            while (allResults.Read())
            {
                Answer currentAnswer = new Answer();
                currentAnswer.queryID = allResults.GetInt32(allResults.GetOrdinal("queryid"));
                currentAnswer.realmID = allResults.GetInt32(allResults.GetOrdinal("realmid"));
                currentAnswer.answerID = allResults.GetInt32(allResults.GetOrdinal("answerid"));
                currentAnswer.answerString = allResults.GetString(allResults.GetOrdinal("answerstring"));
                returnList.Add(currentAnswer);
            }
            return returnList;
            
        }
    }
}
