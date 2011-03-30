'''
Created on Feb 8, 2011

@author: 33As
'''
import unittest
import ExtractionPath
import Badica
from lxml import etree
from StringIO import StringIO

class BTest(unittest.TestCase):
    def case(self, html, xpath):
        ep = ExtractionPath.ExtractionPath(html=html, xpath=xpath)
        return {'html':html, 'xpath':xpath, 'ep':ep}
    
    def setUp(self):
        self.testcase_dict = {0: self.case("<html><body><div>this is a test</div><div>this is a test</div><div>not part of the test</div></body></html>",
                                          "/html/body/div[1]"),
                              1: self.case("<html><body><div>this is a test</div><div>this is a test</div><div>not part of the test</div></body></html>",
                                          "/html/body/div[2]"),
                              2: self.case("<html><body><div> <div> test </div> <div> test </div></div></body></html>",
                                           "/html/body/div/div[1]"),
                              3: self.case("<html><body><div> <div> test </div> <div> test </div></div></body></html>",
                                           "/html/body/div/div[2]"),
                              4: self.case("<html><body><div> test </div> <div> test </div><div> test </div> <div> test </div><div> test </div> <div> test </div></body></html>",
                                           "/html/body/div[1]"),
                              5: self.case("<html><body><div> test </div> <div> test </div><div> test </div> <div> test </div><div> test </div> <div> test </div></body></html>",
                                           "/html/body/div[1]"),
                              6: self.case("<html><body><div><div>test</div><div>test</div></div><div><div>test</div><div>test</div></div></body></html>",
                                           "/html/body/div[1]/div[1]"),
                              7: self.case("<html><body><div><div>test</div><div>test</div></div><div><div>test</div><div>test</div></div></body></html>",
                                           "/html/body/div[2]/div[2]")}
        
        self.testcase=6
        self.test_badica = Badica.Badica()

    def test_learn(self):
        epath_result = self.test_badica.Learn([self.testcase_dict[self.testcase]['ep'],
                                self.testcase_dict[self.testcase+1]['ep']])
        print "Results epath:"
        print epath_result
        print "        xpath:"
        print epath_result.epath_to_xpath()
        
        test_tree = etree.parse(StringIO(self.testcase_dict[self.testcase]['html']))
        elem_expected = test_tree.xpath(self.testcase_dict[self.testcase]['xpath'])
        elem_found = test_tree.xpath(epath_result.epath_to_xpath())
        self.assertTrue(elem_expected[0] in elem_found)
    
    def test_genpath(self):
        pass
    
    def test_genlevel(self):
        pass
    
    def test_genvertex(self):
        pass
    


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.test_learn']
    suite = unittest.TestLoader().loadTestsFromTestCase(BTest)
    unittest.TextTestRunner(verbosity=2).run(suite)