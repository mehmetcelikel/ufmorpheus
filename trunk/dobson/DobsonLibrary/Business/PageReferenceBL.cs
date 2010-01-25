
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class PageReferenceBL
    {
        public long insertPageReference(PageReference currentPageReference)
        {
            using (PageReferenceDA da = new PageReferenceDA())
            {
                return da.insertPageReference(currentPageReference);
            }
        }
        public void updatePageReferenceQuerystring(int prefid, string qs)
        {
            using (PageReferenceDA da = new PageReferenceDA())
            {
                da.updatePageReferenceQuerystring(prefid, qs);
            }
        }
    }
}