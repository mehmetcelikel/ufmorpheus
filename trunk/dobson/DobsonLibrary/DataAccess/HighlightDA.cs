using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;
using Npgsql;

namespace DobsonLibrary.DataAccess
{
    class HighlightDA : BaseDA
    {
        public long insertHighlight(Highlight currentHL)
        {
            object obj = performScalarQuery(SQLQueries.getLastHighlightIdQuery);

            long thisHighlightID = 0L;

            if (obj != DBNull.Value)
                thisHighlightID = Convert.ToInt64(obj) + 1;
            
            performNonQuery(SQLQueries.insertHighlightQuery,
                new string[] { "highlightid", "answerid","beginoffset","endoffset","queryid","startxpath","endxpath","timestamp","url","pagesource","classid","meetpoint" },
                new object[] { Convert.ToInt32(thisHighlightID),currentHL.answerID,currentHL.beginOffset,currentHL.endOffset,
                    currentHL.queryID,currentHL.startxpath,currentHL.endxpath,currentHL.timestamp,currentHL.url,currentHL.pagesource,
                    currentHL.contextClassID,currentHL.meetpoint});
            return thisHighlightID;
        }
    }
}
