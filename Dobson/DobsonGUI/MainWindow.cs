/*This is the main window in Dobson. This window saves all of the data to the database and gathers the data from the user.

*/

using System;

using System.IO;

using System.Collections.Generic;

using System.ComponentModel;

using System.Data;

using System.Drawing;

using System.Linq;

using System.Text;

using System.Windows.Forms;

using System.Xml;

using System.Xml.XPath;

using DobsonLibrary.Business;

using DobsonLibrary.Domain;

using System.Transactions;

using System.Collections;

using System.Web;
using DobsonLibrary.DataAccess;



namespace DobsonGUI
{

    public partial class Main : Form
    {

        //public static string pathForLoggedPages = "C:\\Documents and Settings\\Lucas L\\Application Data\\Mozilla\\Firefox\\Profiles\\3db6lcgq.default\\MCR";

        public string folderPath;

        public List<Realm> allRealms;

        public List<Context> allContexts;

        private static Hashtable contextClasses = new Hashtable();

        private static SsqElementManager queryElementManager;

        private static Hashtable unresolvedInputs;

        public System.Diagnostics.Process firefox;

        private static int previousPageReferenceId;

        private List<SsqElement> tempInputList;

        private List<SsqElement> tempOutputList;

        private string currentTextSelection;

        //Create the window.  Also create an options window and show it to the user.

        //If there is a valid connection then populate some of the necessary data

        public Main()
        {

            InitializeComponent();

            queryElementManager = new SsqElementManager();

            tempInputList = new List<SsqElement>();

            tempOutputList = new List<SsqElement>();

            unresolvedInputs = new Hashtable();

            previousPageReferenceId = -1;

            folderPath = XMLPath();

            if (folderPath == null)

                return;

            OptionsWindow options = new OptionsWindow();


            if (options.ShowDialog(this) == DialogResult.OK)
            {

                DatabaseConnectionBL connectionBL = new DatabaseConnectionBL();

                connectionBL.setConnection(options.currentConnection);

                if (runConnectionTest())
                {

                    setRealmsFromDatabase();

                    setContextsAndClassesFromDatabase();

                    startButton.Enabled = true;

                    connectionBL.CloseConnection();
                }

                else
                {

                    startButton.Enabled = false;

                }

            }

        }

        //Set the realm auto-complete box with the realms from the database

        void setRealmsFromDatabase()
        {

            RealmBL realm = new RealmBL();

            allRealms = realm.getAllRealms();

            AutoCompleteStringCollection realmCollection = new AutoCompleteStringCollection();

            foreach (Realm r in allRealms)
            {

                realmCollection.Add(r.realm);

            }

            realmTextBox.AutoCompleteCustomSource = realmCollection;

        }

        //Set the  contexts from the database to the data grid views

        void setContextsAndClassesFromDatabase()
        {
            //clear things out first
            contextClasses.Clear();

            contextMenuDropDown.Items.Clear();

            outContextMenuDropDown.Items.Clear();

            classMenuDropDown.Items.Clear();

            ContextBL contextBL = new ContextBL();

            allContexts = contextBL.getAllContexts();

            allContexts.Sort(CompareContexts);

            foreach (Context c in allContexts)
            {
                contextMenuDropDown.Items.Add(c.contextName);

                outContextMenuDropDown.Items.Add(c.contextName);
            }

            ContextClassBL ccbl = new ContextClassBL();

            List<ContextClass> classes = ccbl.getAllContextClasses();

            classes.Sort(CompareContextClasses);
            
            foreach (ContextClass c in classes)
            {
                contextClasses.Add(c.name, c);

                classMenuDropDown.Items.Add(c.name);
            }

        }

        private static int CompareContexts(Context x, Context y)
        {
            return x.contextName.CompareTo(y.contextName);
        }
        private static int CompareContextClasses(ContextClass x, ContextClass y)
        {
            return x.name.CompareTo(y.name);
        }

        //set the progress bar along the bottom of the window

        void setWorkingStatus(string status)
        {

            toolStripStatusLabel1.Text = status;

            toolStripProgressBar1.Visible = true;

        }

        void setCompletedStatus(string status)
        {

            toolStripStatusLabel1.Text = status;

            toolStripProgressBar1.Visible = false;

        }

        void clearStatus()
        {

            toolStripProgressBar1.Visible = false;

            toolStripStatusLabel1.Text = "";

        }

        //if the options button is clicked on the menu then show the user a new options window

        private void optionsToolStripMenuItem_Click(object sender, EventArgs e)
        {

            bool runTest = false;

            clearStatus();

            OptionsWindow options = new OptionsWindow();

            if (options.ShowDialog(this) == DialogResult.OK)
            {

                runTest = true;

                DatabaseConnectionBL connectionBL = new DatabaseConnectionBL();

                connectionBL.setConnection(options.currentConnection);

            }

            if (runTest)
            {

                if (runConnectionTest()) startButton.Enabled = true;

            }

        }

        //test the database connection to see if it is valid

        public bool runConnectionTest()
        {

            DatabaseConnectionBL connectionBL = new DatabaseConnectionBL();

            setWorkingStatus("Testing connection");

            if (connectionBL.testConnection())
            {
                
                setCompletedStatus("You have a valid Database connection");

                return true;

            }

            else
            {

                setCompletedStatus("Database connection is not valid");

                return false;

            }

        }

        //get the path where the xml files are being stored at

        public string XMLPath()
        {

            string currentPath = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData) + "\\Mozilla\\Firefox\\Profiles\\";

            string[] filePaths = Directory.GetDirectories(currentPath);

            foreach (string s in filePaths)
            {

                string[] deepFolders = Directory.GetDirectories(s);

                if (deepFolders.Contains(s + "\\MCR"))
                {

                    currentPath = s + "\\MCR";

                    return currentPath;

                }

            }

            //Check to make sure the scraper is installed

            if (File.Exists(currentPath + @"\tf4ihq0m.default\extensions\staged-xpis\mcr@csail.mit.edu\mcr_v4.xpi"))
            {

                String path = currentPath + @"\tf4ihq0m.default\MCR";

                //if so, create MCR folder

                Directory.CreateDirectory(path);

                return path;

            }

            else //Scaper is not installed

                MessageBox.Show("The Scraper Firefox plugin must be installed before you run Dobson\nIt can be" +

                @"downloaded here: http://zion.cise.ufl.edu/svn/scraper/compiled/mcr_v4.xpi");



            return null;

        }

        private void gatherXML(string path, Query thisQuery, int answerid)
        {

            using (TransactionScope scope = new TransactionScope())
            {

                //expression = "/xml/input-field/value";

                string xmlStringStarter = "";

                StreamReader thisFile = new StreamReader(path);

                while (!thisFile.EndOfStream)
                {

                    string line = thisFile.ReadLine();

                    if (line.Contains("<xml>"))
                    {

                        xmlStringStarter = "/xml";

                        break;

                    }

                    else if (line.Contains("<morpheus>"))
                    {

                        xmlStringStarter = "/morpheus";

                        break;

                    }

                }

                thisFile.Dispose();



                if (xmlStringStarter == "")
                {

                    MessageBox.Show(path, "Bad XML File", MessageBoxButtons.OK);



                    throw new Exception("The xml file is missing an appropriate header" +

                            "\nYou may attempt to fix the error in the xml data yourself");

                }



                XPathNodeIterator iterator = null;



                XPathDocument doc = new XPathDocument(path);

                XPathNavigator nav = doc.CreateNavigator();

                string expression;



                expression = xmlStringStarter + "/*";

                iterator = nav.Select(expression);



                if (iterator != null && iterator.MoveNext())
                {



                    if (iterator.Current.Name != "type")
                    {

                        MessageBox.Show("Improperly formatted xml file: " + path + "\nExpected type tag first");

                        throw new Exception("The xml file is missing an appropriate header" +

                            "\nYou may attempt to fix the error in the xml data yourself");

                    }



                    switch (iterator.Current.Value.Trim())
                    {

                        case "link":

                            processPageAndPageReferences(thisQuery, iterator);

                            break;



                        case "form":

                            processPageAndPageReferences(thisQuery, iterator);

                            break;



                        case "highlight":

                            processHighlights(iterator, thisQuery, answerid);

                            break;

                    }



                }

                scope.Complete();

            }

        }



        private static void processHighlights(XPathNodeIterator iterator, Query thisQuery, int answerID)
        {

            Highlight thisHighlight = new Highlight();



            bool isAnswer = false;



            while (iterator.MoveNext())
            {

                switch (iterator.Current.Name)
                {

                    case "time":



                        thisHighlight.timestamp = Int64.Parse(iterator.Current.Value);



                        break;



                    case "url":


                        thisHighlight.url = HttpUtility.UrlDecode(iterator.Current.Value);

                        parseQuerystring(thisHighlight.url);

                        break;



                    case "meetpoint":





                        thisHighlight.meetpoint = iterator.Current.Value;



                        break;



                    case "xpath":



                        string type = iterator.Current.GetAttribute("type", "");



                        if (type == "anchor")

                            thisHighlight.startxpath = iterator.Current.Value;

                        else

                            thisHighlight.endxpath = iterator.Current.Value;



                        break;



                    case "start":



                        thisHighlight.beginOffset = Int32.Parse(iterator.Current.Value);



                        break;



                    case "end":



                        thisHighlight.endOffset = Int32.Parse(iterator.Current.Value);



                        break;



                    case "selection":

                        thisHighlight.selection = HttpUtility.UrlDecode(iterator.Current.Value);            

                        break;


                    case "class":

                        if (iterator.Current.Value != "" && !contextClasses.ContainsKey(iterator.Current.Value))
                        {

                            int id = getMaxContextClassID();

                            ContextClass newClass = new ContextClass(++id, 1, iterator.Current.Value);

                            ContextClassBL.insertContextClass(newClass);

                            contextClasses[iterator.Current.Value] = newClass;

                        }

                        else if (iterator.Current.Value == "")

                            throw new Exception("A highlight has an invalid context class, cannot insert query into database");


                        ContextClass c = (ContextClass)contextClasses[iterator.Current.Value];

                        thisHighlight.contextClassID = c.classId;


                        break;
                        

                    case "page":

                        
                        thisHighlight.pagesource = HttpUtility.UrlDecode(iterator.Current.Value.Trim());
                        

                        break;



                    case "answer":



                        isAnswer = bool.Parse(iterator.Current.Value);



                        break;

                }





            }



            //Need to ensure that xpaths and offset are in proper direction, not reversed

            if (thisHighlight.endxpath.Length < thisHighlight.startxpath.Length)
            {

                string t = thisHighlight.startxpath;

                thisHighlight.startxpath = thisHighlight.endxpath;

                thisHighlight.endxpath = t;

            }

            else if (thisHighlight.endxpath.Length == thisHighlight.startxpath.Length)
            {

                string endIndex = thisHighlight.endxpath.Substring(thisHighlight.endxpath.Length - 4);



                string startindex = thisHighlight.startxpath.Substring(thisHighlight.startxpath.Length - 4);



                if (endIndex[0] == '[' && endIndex[2] == ']' && startindex[0] == '[' && startindex[2] == ']')

                    if ((int)endIndex[1] < (int)startindex[1])
                    {

                        string t = thisHighlight.startxpath;

                        thisHighlight.startxpath = thisHighlight.endxpath;

                        thisHighlight.endxpath = t;

                    }

            }



            if (thisHighlight.beginOffset > thisHighlight.endOffset)
            {

                int t = thisHighlight.beginOffset;

                thisHighlight.beginOffset = thisHighlight.endOffset;

                thisHighlight.endOffset = t;

            }

            //Insert highlight

            HighlightBL highlightbiz = new HighlightBL();

            thisHighlight.queryID = thisQuery.queryID;

            //If the current highlight is an answer, then set its answerid

            if (isAnswer)

                thisHighlight.answerID = answerID;

            long highlightid = highlightbiz.insertHighlight(thisHighlight);

            //add this highlight to itermediate structure for comparison against form inputs
            string tag = thisHighlight.selection.ToUpper();

            thisHighlight.highlightID = (Int32)highlightid;

            queryElementManager.AddHighlight(new SsqElement(tag, thisHighlight.contextClassID, thisHighlight.highlightID, null));

        }

        private static void parseQuerystring(string url)
        {
            int index = url.IndexOf("?");

            //bool isgoogle = false;

            //if (index == -1)
            //{
            //    index = url.IndexOf("/#");
            //    isgoogle = true;
            //}

            if (index != -1)
            {
                ////the delimeter for google querystring is longer than the standard one so increment
                //if (isgoogle)
                //    index++;

                PageReferenceBL prefbiz = new PageReferenceBL();

                string qs = url.Substring(index, url.Length - index);

                //The querystring on fthis page is actually for the previous page reference
                //so we need to update the database to reflect this

                if (previousPageReferenceId != -1)
                {
                    int NAME = 0;

                    int VALUE = 0;

                    prefbiz.updatePageReferenceQuerystring(previousPageReferenceId, qs);

                    // Check if any pending input object inserts are waiting on data from 
                    //this querystring
                    Hashtable elems = (Hashtable)unresolvedInputs[previousPageReferenceId];

                    //split the querystring into substrings which consist of name/value pairs
                    string[] nameValuePairs = qs.Split(new char[] { '&' });

                    InputBL inputBl = new InputBL();

                    //iterate through the pairs and attempt to match the names to outstanding inputs
                    foreach (string nvp in nameValuePairs)
                    {
                        if (elems == null)
                            continue;

                        //split the string around the equals so now we have the name and value
                        string[] subs = nvp.Split(new char[] { '=' });

                        Input i = (Input)elems[subs[NAME]];

                        //if we find an input with the given name, attempt to find a matching ssq input
                        if (i != null)
                        {
                            SsqElement storedElement = (SsqElement)queryElementManager.FindSsqInput(i.contextClassID, subs[VALUE]);

                            //if we have a valid ssq input, then insert the input into the db with
                            //the newly found individual id
                            if (storedElement != null)
                            {
                                i.individualID = storedElement.IndividualId;

                                inputBl.insertInput(i);
                            }
                            else//see if there is a highlight which is unresolved
                            {
                                storedElement = (SsqElement)queryElementManager.FindHighlight(i.contextClassID, subs[VALUE]);

                                //if we have found a highlight with a matching value, then insert the input with 
                                //the highlight id
                                if (storedElement != null)
                                {
                                    i.highlightID = storedElement.IndividualId;

                                    inputBl.insertInput(i);
                                }
                            }

                        }
                    }

                    //Because we've now checked the querystring, we can clear out the unresolved
                    //input hash
                    unresolvedInputs.Clear();

                }

            }
        }



        private static int getMaxContextClassID()
        {

            int maxContextClassid = -1;

            foreach (ContextClass c in contextClasses.Values)
            {

                if (c.classId > maxContextClassid)

                    maxContextClassid = c.classId;

            }

            return maxContextClassid;

        }



        private static void processPageAndPageReferences(Query thisQuery, XPathNodeIterator iterator)

        {

            Page thisPage = new Page();

            PageReference pref = new PageReference();

            PageBL pagebiz = new PageBL();

            PageReferenceBL prefbiz = new PageReferenceBL();

            string[] inputs = null;



            //Traverse document and save necessary pieces of data

            while (iterator.MoveNext())

            {

                switch (iterator.Current.Name)
                {

                    case "url":



                        string url = HttpUtility.UrlDecode(iterator.Current.Value);

                        int index = url.IndexOf("?");

                        if (index == -1)
                            index = url.IndexOf("/#");

                        if (index >= 0)
                            thisPage.baseURL = url.Substring(0, index);
                        else
                            thisPage.baseURL = url;

                        pref.queryString = "";

                        parseQuerystring(url);

                        break;

                    case "time":



                        pref.timestamp = Int64.Parse(iterator.Current.Value);



                        break;



                    case "xpath":



                        pref.formxpath = iterator.Current.Value;



                        break;



                    case "inputlist":


                        inputs = iterator.Current.Value.Split(new string[] { "%2C" }, StringSplitOptions.RemoveEmptyEntries);

                        List<string> inputList = new List<string>();

                        if (inputs == null)// || inputs.Length % 2 != 0)
                            throw new Exception("Page data for " + thisPage.baseURL + " has missing input information. The scraper has left out one or more input names.");
                        else if (inputs.Length % 2 != 0)
                            //iterate by pairs and move all pairs into the input list
                        break;



                    case "node":



                        pref.destinationUrl = getDestinationUrl(HttpUtility.UrlDecode(iterator.Current.Value));



                        break;



                    case "page":



                        string s = iterator.Current.Value.Trim();



                        pref.pagesource = HttpUtility.UrlDecode(s);



                        break;

                }

            }

            //Insert the page object

            thisPage.pageID = Convert.ToInt32(pagebiz.insertPage(thisPage));

            //Insert the page reference

            pref.pageID = thisPage.pageID;

            pref.queryID = thisQuery.queryID;

            pref.pageRefID = Convert.ToInt32(prefbiz.insertPageReference(pref));

            //set this so that we may find it again if we find a querystring on the subsequent page
            previousPageReferenceId = pref.pageRefID;

            //Insert the inputs

            InputBL inputbiz = new InputBL();


            if (inputs == null)

                return;


            //Get the current max contextclass id

            int maxContextClassid = getMaxContextClassID();

            //create xpath nav objects
            HtmlAgilityPack.HtmlDocument document = new HtmlAgilityPack.HtmlDocument();

            document.LoadHtml(pref.pagesource);

            PhraseBL individualUpdater = new PhraseBL();

            for (int cnt = 0; cnt < inputs.Length && inputs[0] != "null";)
            {
                //must decode the input because it has been encoded
                string inputName = HttpUtility.UrlDecode(inputs[cnt]);

                //must also decode class name, just in case
                string className = HttpUtility.UrlDecode(inputs[cnt+1]);

                ContextClass contextClass = (ContextClass)contextClasses[className];

                Int32 classId = 0;

                if(contextClass == null)
                {

                    ContextClass newclass = new ContextClass(++maxContextClassid, 1, className);

                    ContextClassBL.insertContextClass(newclass);

                    string formattedKey = Main.formatKey(className);

                    contextClasses[formattedKey] = contextClass;

                    classId = maxContextClassid;

                }
                else 
                    classId = contextClass.classId;

                //DEBUG only 
                if (!pref.formxpath.StartsWith("/HTML/BODY"))
                    pref.formxpath = "/html/body" + pref.formxpath;
                    //pref.formxpath = "/" + pref.formxpath;

                //Get the input value from the pagesource
                HtmlAgilityPack.HtmlNode parentNode = document.DocumentNode.SelectSingleNode(pref.formxpath.ToLower());

                SsqElement elem = null;

                string txt = "";
                
                if (parentNode != null)
                {
                    HtmlAgilityPack.HtmlNode selectedNode = parentNode.ParentNode.SelectSingleNode("//input[@name='" + inputName + "']");

                    if (selectedNode != null)
                    {
                        //find matching ssq input if there is one
                        if (selectedNode.Attributes["value"] != null)
                            txt = selectedNode.Attributes["value"].Value.ToUpper();
                        else if (selectedNode.Attributes["title"] != null)
                            txt = selectedNode.Attributes["title"].Value.ToUpper();

                        elem = queryElementManager.FindSsqInput(classId, txt);
                    }
                }

                int individ = -1;

                int highlightid = -1;

                if (elem != null)
                {
                    individ = elem.IndividualId;
                }
                else
                {
                    elem = queryElementManager.FindHighlight(classId, txt);

                    //if there is a highlight with this txt, then get it and update the input
                    if(elem != null)
                        highlightid = elem.IndividualId;
                }
                
                Input newi = new Input(0, inputName, pref.pageRefID, classId, individ, highlightid);

                //If this input is either form a highlight or an ssq input then go ahead and insert it into db
                if (highlightid != -1 || individ != -1)
                    inputbiz.insertInput(newi);
                else
                {  //if no individual id (or highlightid) has been found for this input, we'll wait for the
                    //querystring to be processed

                    Hashtable elems = (Hashtable)unresolvedInputs[pref.pageRefID];

                    if (elems == null)
                        elems = new Hashtable();

                    elems.Add(newi.name,newi);

                    unresolvedInputs[pref.pageRefID] = elems;

                }
                cnt += 2;

            }

        }

        private static string getDestinationUrl(string url)
        {

            int index = url.IndexOf("action=\"");



            if (index != -1)

                index += 8;

            else
            {

                index = url.IndexOf("href=\"");



                if (index != -1)

                    index += 6;

                else

                    return "";

            }



            StringBuilder sb = new StringBuilder();



            while (url[index] != '\"' && index < url.Length)

                sb.Append(url[index++]);



            return sb.ToString();

        }

        private static void writeOutPopupData(string path)
        {

            try
            {

                path = path.Substring(0, path.LastIndexOf(@"\MCR"));

                if (!Directory.Exists(path + @"..\mcrdb"))

                    Directory.CreateDirectory(path + @"..\mcrdb");

                StreamWriter writer = new StreamWriter(path + @"..\mcrdb\contextclasslist.txt", false);

                List<string> sortedList = sortStrings(contextClasses.Keys);

                foreach (string s in sortedList)
                {
                   writer.WriteLine(s);
                }

                writer.Close();

            }

            catch (IOException e)
            {

                MessageBox.Show(e.Message);

            }

        }

        private static List<string> sortStrings(ICollection list)
        {
            List<string> sortedList = new List<string>(list.Count);

            foreach (string k in list)
            {
                int cnt =0;
                for (; cnt < sortedList.Count; cnt++)
                {
                    if (sortedList[cnt].CompareTo(k) > 0)
                    {
                        break;
                    }
                }

                sortedList.Insert(cnt, k);

            }

            return sortedList;
        }

        private void startButton_Click(object sender, EventArgs e)
        {
            
            if (startButton.Text == "START")
            {

                if (realmTextBox.Text == "")
                {

                    MessageBox.Show("Realm has to be set before starting.", "Incomplete data", MessageBoxButtons.OK);

                }

                else
                {
                    DatabaseConnectionBL bl = new DatabaseConnectionBL();
                    bl.OpenConnection();

                    //write out the classes for the scraper
                    writeOutPopupData(folderPath);
                    
                    clearXMLData();

                    startButton.Text = "STOP";

                    setWorkingStatus("Gathering Data");

                    firefox = System.Diagnostics.Process.Start("firefox.exe", "www.google.com");

                }

            }

            else
            {

                if (MessageBox.Show("Are you done with your search?\nIs all of the information filled in?", "Confirm stop", MessageBoxButtons.YesNo) == DialogResult.Yes)
                {

                    if (queryTextBox.Text == "" || answerTextBox.Text == "")
                    {

                        setWorkingStatus("Incomplete data");

                    }

                    else
                    {

                        bool excep = false;



                        try
                        {

                            startButton.Text = "START";

                            setWorkingStatus("Saving Data");

                            saveData();

                            setCompletedStatus("Data Saved");

                            clearXMLData();

                            setRealmsFromDatabase();

                        }

                        catch (Exception ex)
                        {

                            MessageBox.Show(ex.Message+" "+ex.StackTrace, "An exception has occurred", MessageBoxButtons.OK, MessageBoxIcon.Error);

                            excep = true;

                        }

                        if (excep)
                            startButton.Text = "STOP";

                        if (MessageBox.Show("Do you want to clear out this form?", "Clear Data", MessageBoxButtons.YesNo) == DialogResult.Yes)
                        {

                            clearForm();
                            startButton.Text = "START";
                        }
                        
                        setRealmsFromDatabase();

                        setContextsAndClassesFromDatabase();

                        queryElementManager.Clear();

                        unresolvedInputs.Clear();

                    }

                }

            }

        }

        private void clearForm()
        {

            realmTextBox.Text = "";

            queryTextBox.Text = "";

            answerTextBox.Text = "";

            inputListBox.Items.Clear();

            outputListBox.Items.Clear();

            tempInputList.Clear();

            tempOutputList.Clear();

            clearStatus();
        }

        private void clearXMLData()
        {

            string[] files = Directory.GetFiles(folderPath);

            foreach (string file in files)

                File.Delete(file);

        }

        private void saveData()
        {
            string realmString = realmTextBox.Text.Trim();

            int realmID = -1;

            string queryString = queryTextBox.Text.Trim();

            Query thisQuery;

            string answerString = answerTextBox.Text.Trim();

            Answer thisAnswer;



            foreach (Realm realmCheck in allRealms)
            {

                if (realmCheck.realm == realmString)
                {

                    realmID = realmCheck.realmid;

                    break;

                }

            }

            if (realmID == -1)
            {

                RealmBL realmBL = new RealmBL();

                realmID = (int)realmBL.insertRealm(realmString);

            }

            QueryBL queryBL = new QueryBL();

            thisQuery = new Query(queryString, realmID, 0);

            thisQuery.queryID = (int)queryBL.insertQuery(thisQuery);

            //Insert answer into db

            AnswerBL answerBL = new AnswerBL();

            thisAnswer = new Answer(answerString, thisQuery.queryID, realmID);

            thisAnswer.answerID = (int)answerBL.insertAnswer(thisAnswer);

            //save the query input data
            saveContextInfo(thisQuery);             

            //Get the list of files outputted from the scraper

            string[] files = Directory.GetFiles(folderPath);

            files = sortByFileNumber(files);

            //Run through each file and handle the page references, highlights, etc. 

            foreach (string file in files)
            {

                gatherXML(file, thisQuery, thisAnswer.answerID);

            }

            //if there are still some unresolved inputs then these are simply constant values
            //but they still must be inserted into the db
            InputBL bl = new InputBL();
            foreach (Int32 hashkey in unresolvedInputs.Keys)
            {
                Hashtable ihash = (Hashtable)unresolvedInputs[hashkey];

                foreach(string k in ihash.Keys)
                    bl.insertInput((Input)ihash[k]);
            }
        }

        private string[] sortByFileNumber(string[] files)
        {
            for (int i = 0; i < files.Length - 1; i++)
            {
                for (int j = 1; j < files.Length; j++)
                {
                    //if the ith file has a greater ordinal than the jth file, then swap
                    if (compareOrdinal(files[i], files[j]) > 0)
                    {
                        string t = files[j];
                        files[j] = files[i];
                        files[i] = t;
                    }
                }
            }

            return files;
        }
        private int compareOrdinal(string s, string r)
        {
            int si = s.IndexOf("-");
            int ri = r.IndexOf("-");

            int sSlashIndex = s.LastIndexOf("\\")+1;
            int rSlashIndex = r.LastIndexOf("\\")+1;

            Int32 snum = Int32.Parse(s.Substring(sSlashIndex, si-sSlashIndex));
            Int32 rnum = Int32.Parse(r.Substring(rSlashIndex, ri-rSlashIndex));

            if (snum > rnum)
                return 1;
            else if (snum == rnum)
                return 0;
            else
                return -1;

        }

        public void saveContextInfo(Query thisQuery)
        {
            ContextBL bl = new ContextBL();

            //Handle inputs first, load them into the queryElementManager
            foreach (SsqElement e in tempInputList)
            {

                //insert a new context
                if(e.ElementContext.contextID == -1)
                    bl.insertContext(e.ElementContext);

                Phrase phrase = new Phrase(e.Individual, e.ElementContext.contextID, e.ClassId);

                PhraseBL phraseBl = new PhraseBL();

                phrase = phraseBl.insertPhraseWithIndividual(phrase);

                e.IndividualId = phrase.individualID;

                //add ssq input element manager
                queryElementManager.AddSsqInput(e);

                IndividualLink queryLink = new IndividualLink(e.IndividualId, thisQuery, "input");

                IndividualLinkBL linkBl = new IndividualLinkBL();

                linkBl.insertIndividualLink(queryLink);

                InsertModifiers(e.Modifiers, e.IndividualId);
            }
            
            //we get this ahead of time for use during the foreach loop below
            int maxClassId = getMaxContextClassID();

            //Handle outputs, add them to queryManager
            foreach (SsqElement e in tempOutputList)
            {
                //outputs are already classes, so try to find the class id for the current string
                //if none is found, then insert a new class
                string formattedKey = Main.formatKey(e.Individual);

                ContextClass c = (ContextClass)contextClasses[formattedKey];

                //if null then this is a new class, so we must insert it into the database 
                if (c == null)
                {

                    c = new ContextClass(++maxClassId, e.ElementContext.contextID, formattedKey);

                    ContextClassBL.insertContextClass(c);

                    contextClasses[formattedKey] = c;
                }

                if (e.ElementContext.contextID == -1)
                    bl.insertContext(e.ElementContext);

                Phrase phrase = new Phrase(e.Individual, e.ElementContext.contextID, c.classId);

                PhraseBL PhraseBl = new PhraseBL();

                phrase = PhraseBl.insertPhraseWithIndividual(phrase);

                e.IndividualId = phrase.individualID;

                IndividualLink queryLink = new IndividualLink(e.IndividualId, thisQuery, "output");

                IndividualLinkBL linkBl = new IndividualLinkBL();

                linkBl.insertIndividualLink(queryLink);

                InsertModifiers(e.Modifiers, e.IndividualId);
            }

        }

        private void InsertModifiers(List<Modifier> list, int individualId)
        {

            //first determine rank of modifiers
            List<Modifier> sortedList = new List<Modifier>();
            
            ModifierBL biz = new ModifierBL();

            string[] query = queryTextBox.Text.Split(new char[] { ' ' });

            int rank = 1;

            foreach (string s in query)
            {
                Modifier mod = null;

                foreach (Modifier possible in list)
                    if (possible.ModifierString == s)
                        mod = possible;

                if (mod == null)
                    continue;

                mod.Rank = rank;

                mod.IndividualId = individualId;

                biz.insertModifier(mod);

                list.Remove(mod);

                rank++;
            }
        }

      

        private static string formatKey(string p)
        {
            if(p.Length == 0)
                return "";

            string temp = "";

            int asciValue = (int)p[0];

            //if the leading character is lowercase, make it uppercase
            if (asciValue >= 97)
                temp += (char)(asciValue - 32);
            else
                temp += (char)asciValue;

            temp +=  p.Substring(1).ToLower();

            return temp;
        }

        private bool containsAlready(SsqElement cmp, List<SsqElement> list)
        {

            foreach (SsqElement e in list)
            {
                if (cmp.Compare(e))
                    return true;                    
            }

            return false;
        }

      
        private void removeButton_Click(object sender, EventArgs e)
        {
            if (inputListBox.SelectedIndex >= 0)
            {
                tempInputList.RemoveAt(inputListBox.SelectedIndex);

                inputListBox.Items.RemoveAt(inputListBox.SelectedIndex);
            }
            else if (outputListBox.SelectedIndex >= 0)
            {
                tempOutputList.RemoveAt(outputListBox.SelectedIndex);

                outputListBox.Items.RemoveAt(outputListBox.SelectedIndex);

            }
        }

        private void Main_Load(object sender, EventArgs e)
        {
            
        }

        private void queryTextBox_Click(object sender, EventArgs e)
        {

            if (queryTextBox.Text.Length > 0 && queryTextBox.SelectionStart < queryTextBox.Text.Length && 
                queryTextBox.Text[queryTextBox.SelectionStart] != ' ')
            {
               
                int start = 0;

                int index = queryTextBox.SelectionStart -1;

                if(index >= 0 && queryTextBox.Text[index] != ' ')
                {
                    //get the start of the word
                    for(; index >= 0;index--)
                    {
                        if(queryTextBox.Text[index] == ' ')
                            break;
                    }
                }

                start = index + 1;

                index = queryTextBox.SelectionStart;

                int end = queryTextBox.SelectionStart + queryTextBox.SelectionLength-1;
              
                for(; index < queryTextBox.Text.Length; index++)
                {
                    if (index >= end && (queryTextBox.Text[index] == ' ' || Char.IsPunctuation(queryTextBox.Text[index])))
                        break;
                }

                //get the word
                this.currentTextSelection = queryTextBox.Text.Substring(start, index - start);

                //show menu popup
                selectionMenu.Show(Cursor.Position);

            }
        }

        private void inputDoneToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (classMenuDropDown.Text == "Class")
            {
                MessageBox.Show("Please select a class");
                return;
            }

            ContextClass contextClass = (ContextClass)contextClasses[classMenuDropDown.Text];

            if (contextClass == null)
            {
                contextClass = new ContextClass(-1, -1, classMenuDropDown.Text);

                contextClasses[classMenuDropDown.Text] = contextClass;
            }

            string context = contextMenuDropDown.Text.Trim();

            Context c = findContext(contextMenuDropDown.SelectedIndex,context);

            if (c == null)
                c = new Context(-1, context);

            SsqElement elem = new SsqElement(this.currentTextSelection, contextClass.classId,-1, c);

            if (containsAlready(elem, tempInputList))
            {
                MessageBox.Show("Duplicate inputs are not permitted.");
                return;
            }

            tempInputList.Add(elem);

            inputListBox.Items.Add(c.contextName + ", " + contextClass.name + ", " + this.currentTextSelection);

            updateSubjectList();
        }

        private void outputDoneToolStripMenuItem_Click(object sender, EventArgs e)
        {
            string context = outContextMenuDropDown.Text.Trim();

            Context c = null;

            if(outContextMenuDropDown.Text == "Context")
            {
                MessageBox.Show("Please select a context");
                return;
            }

            c = findContext(outContextMenuDropDown.SelectedIndex,context);                        

            if(c == null)
                c = new Context(-1, context);

            //need to format valueText before searching the hashtable because all its keys follow a pattern
            string formattedKey = Main.formatKey(this.currentTextSelection);

            ContextClass contextClass = (ContextClass)contextClasses[formattedKey];

            int tempId = -1;

            if (contextClass != null)
                tempId = contextClass.classId;
            
            SsqElement elem = new SsqElement(this.currentTextSelection, tempId,-1, c);

            if (containsAlready(elem, tempOutputList))
            {
                MessageBox.Show("Duplicate outputs are not permitted.");
                return;
            }

            tempOutputList.Add(elem);

            outputListBox.Items.Add(outContextMenuDropDown.SelectedItem + ", " + this.currentTextSelection);

            updateSubjectList();
        }

        private Context findContext(int i, string context)
        {
            if (i == -1)
            {
                foreach (Context con in allContexts)
                    if (con.contextName.Equals(context, StringComparison.CurrentCultureIgnoreCase))
                    {
                        return con;
                    }
                return null;
            }
            
            return (Context)allContexts[i];
        }

        private void updateSubjectList()
        {
            subjectMenuDropDown.Items.Clear();

            //fill subject word list
            if (tempInputList.Count > 0)
                foreach (SsqElement e in tempInputList)
                    subjectMenuDropDown.Items.Add(e.Individual);

            if (tempOutputList.Count > 0)
                foreach (SsqElement e in tempOutputList)
                    subjectMenuDropDown.Items.Add(e.Individual);
        }

        private void modifierDoneToolStripMenuItem_Click(object sender, EventArgs e)
        {
            if (subjectMenuDropDown.SelectedIndex == -1)
                return;

            int index = subjectMenuDropDown.SelectedIndex;

            bool isInput = false;

            SsqElement elem = null;

            if (index < tempInputList.Count)
            {
                elem = tempInputList[index];

                isInput = true;
            }
            else
            {
                index -= tempInputList.Count;

                elem = tempOutputList[index];
            }

            elem.Modifiers.Add(new Modifier(-1, this.currentTextSelection, -1, -1));

            if (isInput)
            {
                string currentText = (string)inputListBox.Items[index];

                inputListBox.Items.RemoveAt(index);

                inputListBox.Items.Insert(index, currentText + " (" + this.currentTextSelection + ")");
            }
            else
            {
                //index--;

                string currentText = (string)outputListBox.Items[index];

                outputListBox.Items.RemoveAt(index);

                outputListBox.Items.Insert(index, currentText + " (" + this.currentTextSelection + ")");

            }

        }

        
        private void outCancelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            selectionMenu.Close();
        }

        private void modifierCancelToolStripMenuItem_Click(object sender, EventArgs e)
        {
            selectionMenu.Close();
        }

        private void outCancelToolStripMenuItem_Click_1(object sender, EventArgs e)
        {
            selectionMenu.Close();
        }

        private void cleanText()
        {
            int lastChar = queryTextBox.Text.Length-1;

            if (char.IsPunctuation(queryTextBox.Text[lastChar]))
                queryTextBox.Text = queryTextBox.Text.Substring(0, lastChar);
        }

        private void inputListBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (inputListBox.SelectedIndex >= 0)
                outputListBox.SelectedIndex = -1;
        }

        private void outputListBox_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (outputListBox.SelectedIndex >= 0)
                inputListBox.SelectedIndex = -1;
        }
      
    }

}





