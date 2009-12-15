using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using DobsonLibrary.DataAccess;
using DobsonLibrary.Domain;

namespace DobsonLibrary.Business
{
    public class WebsiteBL
    {

        public IList<Website> getFilteredWebsites(string searchString)
        {
            using (WebsiteDA da = new WebsiteDA())
            {
                return da.getFilteredWebsites(searchString);
            }
        }
    }
}
