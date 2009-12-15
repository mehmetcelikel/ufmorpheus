using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class ContextClassBL
    {
        public List<ContextClass> getAllContextClasses()
        {
            using (ContextClassDA cc = new ContextClassDA())
            {
                return cc.getContextClassList();
            }
        }

        public static void insertContextClass(DobsonLibrary.Domain.ContextClass newclass)
        {
            using (ContextClassDA cc = new ContextClassDA())
                cc.insertContextClass(newclass);
        }
    }
}
