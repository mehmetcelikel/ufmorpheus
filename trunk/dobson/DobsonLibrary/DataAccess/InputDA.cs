using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.Domain;

namespace DobsonLibrary.DataAccess
{
    class InputDA : BaseDA
    {
        public long insertInput(Input currentInput)
        {
            object id = performScalarQuery(SQLQueries.getLastInputIdQuery);

            long inputid = 0L;

            if (id != DBNull.Value)
                inputid = Convert.ToInt64(id) + 1;

            performNonQuery(SQLQueries.insertInputQuery,
               new string[] { "inputid", "name", "classid", "pagerefid","individualID","highlightID"},
               new object[] {Convert.ToInt32(inputid), currentInput.name,currentInput.contextClassID,
                   currentInput.pageRefID,currentInput.individualID,currentInput.highlightID});

            return inputid;
        }
    }
}
