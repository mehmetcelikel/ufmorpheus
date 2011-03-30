'''
Created on Feb 8, 2011

@author: 33As
'''
import unittest
import Vertex



class VertexTest(unittest.TestCase):

    def setUp(self):
        self.vertex_fromstring = Vertex.Vertex()
        self.vertex_addlabel = Vertex.Vertex('f')
        self.vertex_discardlabel = Vertex.Vertex('f')
        self.vertex_hastag = Vertex.Vertex('f')
        self.vertex_hastag.addLabel('div')
        self.vertex_gettag = Vertex.Vertex('l')
        self.vertex_gettag.addLabel('div')
        self.vertex_intersect = Vertex.Vertex()
        

    def test_fromstring(self):
        #Test the fromstring function
        #TODO: It has an eval within, easily exploitable
        self.vertex_fromstring.fromstring("['f', 'l', 'div']")
        self.assertEqual(self.vertex_fromstring.label, set(['f', 'l', 'div']))

    def test_addLabel(self):
        #Add a label
        self.vertex_addlabel.addLabel('l')
        self.assertEqual(self.vertex_addlabel.label, set(['f', 'l']))
        
        #Add a label already in the set
        self.vertex_addlabel.addLabel('f')
        self.assertEqual(self.vertex_addlabel.label, set(['f', 'l']))
        
    
    def test_discardLabel(self):
        #Discard a label
        self.vertex_discardlabel.discardLabel('f')
        self.assertEqual(self.vertex_discardlabel.label, set([]))
        
        #Discard a label not in the set
        self.vertex_addlabel.addLabel('f')
        self.assertEqual(self.vertex_discardlabel.label, set([]))
    
    def test_hasTag(self):
        #Checks to see if there is another tag besides f, l
        self.assertTrue(self.vertex_hastag.hasTag())
    
    def test_getTag(self):
        self.assertEqual(self.vertex_gettag.getTag(), 'div')
        
    def test_intersect(self):
        self.vertex_intersect.intersect(self.vertex_hastag, self.vertex_gettag)
        self.assertEqual(self.vertex_intersect.label, set(['div']))

if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    #unittest.main()
    suite = unittest.TestLoader().loadTestsFromTestCase(VertexTest)
    unittest.TextTestRunner(verbosity=2).run(suite)
