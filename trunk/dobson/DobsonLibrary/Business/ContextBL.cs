using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class ContextBL
    {
        public int insertContext(Context c)
        {
            using (ContextDA da = new ContextDA())
            {
                return da.insertContext(c);
            }
        }
        public List<Context> getAllContexts()
        {
            using (ContextDA da = new ContextDA())
            {
                return da.getAllContexts();
            }
        }

    }
}
