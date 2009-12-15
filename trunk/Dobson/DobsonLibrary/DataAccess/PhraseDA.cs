
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class PhraseDA : BaseDA
    {
        public Phrase insertPhraseWithIndividual(Phrase currentPhrase)
        {
            Phrase returnPhrase = new Phrase(currentPhrase.phraseString, currentPhrase.contextID, currentPhrase.classID);
            
            object obj = performScalarQuery(SQLQueries.getLastPhraseIdQuery);

            returnPhrase.phraseID = 1;

            if (obj != DBNull.Value)
                returnPhrase.phraseID += Convert.ToInt32(obj);
            
            performNonQuery(SQLQueries.insertPhraseQuery,
                            new string[] { "phrasestring", "phraseid" },
                            new object[] { returnPhrase.phraseString, returnPhrase.phraseID });

            performNonQuery(SQLQueries.insertPhraseBelongsToContextQuery,
                            new string[] { "contextid", "phraseid" },
                            new object[] { returnPhrase.contextID, returnPhrase.phraseID });

            performNonQuery(SQLQueries.insertIndividualQuery,
                            new string[] { "phraseid", "classid" },
                            new object[] { returnPhrase.phraseID, returnPhrase.classID});

            returnPhrase.individualID = Convert.ToInt32(performScalarQuery(SQLQueries.getLastIndividualIdQuery));

            return returnPhrase;

        }

    }
}