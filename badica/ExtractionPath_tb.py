'''
Created on Feb 8, 2011

@author: 33As
'''
import unittest
import ExtractionPath
import Level
import Vertex
from lxml import etree
from StringIO import StringIO

class EPTest(unittest.TestCase):
    def case(self, h, l, r, html, xpath):
        ep = ExtractionPath.ExtractionPath(html=html, xpath=xpath)
        return {'h':h, 'l':l, 'r':r, 'html':html, 'xpath':xpath, 'ep':ep}
    
    def setUp(self):
        '''Rough sketch of how to make a more thorough test suite'''
        self.test_html = "<html><body><div>this is a test</div><div>not part of the test</div></body></html>"
        self.test_xpath = "/html/body/div[1]"
        self.extract_ep1 = ExtractionPath.ExtractionPath(html=self.test_html, xpath=self.test_xpath)
        self.testcase_dict = {0: self.case(3, 0, 2,
                                           "<html><body><div>this is a test</div><div>not part of the test</div></body></html>",
                                          "/html/body/div[1]")}
        self.testcase=0;
        self.vertex_cond = Vertex.Vertex('l')
        self.vertex_cond.addLabel('div')
        self.vertex_cond.addLabel('f')
    
    def test_height(self):
        self.assertEqual(self.testcase_dict[self.testcase]['ep'].height(), self.testcase_dict[self.testcase]['h'])
        #self.assertEqual(self.extract_ep1.height(), 3) #ep1

    def test_left(self):
        self.assertEqual(self.testcase_dict[self.testcase]['ep'].left(), self.testcase_dict[self.testcase]['l'])
        #self.assertEqual(self.extract_ep1.left(), 0) #ep1
    
    def test_right(self):
        self.assertEqual(self.testcase_dict[self.testcase]['ep'].right(), self.testcase_dict[self.testcase]['r'])
        #self.assertEqual(self.extract_ep1.right(), 2) #ep1
    
    def test_level(self):
        #TODO
        pass
    
    def test_epath_to_xpath(self):
        xpath = self.testcase_dict[self.testcase]['ep'].epath_to_xpath()
        test_tree = etree.parse(StringIO(self.testcase_dict[self.testcase]['html']))
        elem_expected = test_tree.xpath(self.testcase_dict[self.testcase]['xpath'])
        elem_found = test_tree.xpath(xpath)
        self.assertEqual(elem_found, elem_expected)
        #xpath = self.extract_ep1.epath_to_xpath()
        #test_tree = etree.parse(StringIO(self.test_html))
        #elem_expected = test_tree.xpath(self.test_xpath)
        #elem_found = test_tree.xpath(xpath)
        #self.assertEqual(elem_found, elem_expected)
        
    def test_cond(self):
        xc = self.extract_ep1.cond(self.vertex_cond)
        xc_expected = "[local-name()='"+self.vertex_cond.getTag()+"']"+"[not (preceding-sibling::*)]"+"[not (following-sibling::*)]"
        self.assertEqual(xc, xc_expected)
        
    def test_xpath_to_epath(self):
        #TODO
        pass
    

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testheight']
    #unittest.main()
    suite = unittest.TestLoader().loadTestsFromTestCase(EPTest)
    unittest.TextTestRunner(verbosity=2).run(suite)
