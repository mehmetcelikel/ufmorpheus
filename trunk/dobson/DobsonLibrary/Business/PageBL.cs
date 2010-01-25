
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class PageBL
    {
        public long insertPage(Page currentPage)
        {
            using (PageDA da = new PageDA())
            {
                return da.insertPage(currentPage);
            }
        }
    }
}