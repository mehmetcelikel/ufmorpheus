'''
Created on Feb 8, 2011

@author: 33As
'''
import unittest
import Level
import Vertex

class LevelTest(unittest.TestCase):

    def setUp(self):
        self.level_test_add = Level.Level()
        self.level_test_get = Level.Level()
        
        self.vertex_last = Vertex.Vertex('l')
        self.vertex_first = Vertex.Vertex('f')
    
        self.level_test_get.addLeft(self.vertex_first)
        self.level_test_get.addRight(self.vertex_last)

    
    def test_addleft(self):
        self.level_test_add.addLeft(self.vertex_first)
        self.assertEqual(self.level_test_add.index, 0)
        self.assertEqual(self.level_test_add.vertex[0], self.vertex_first)
    
    def test_addright(self):
        self.level_test_add.addRight(self.vertex_last)
        self.assertEqual(self.level_test_add.index, 0)
        self.assertEqual(self.level_test_add.vertex[0], self.vertex_last)
        
    def test_getright(self):
        self.assertEqual(self.level_test_get.getRight(0), self.vertex_first)
        self.assertEqual(self.level_test_get.getRight(1), self.vertex_last)
    
    def test_getleft(self):
        self.assertEqual(self.level_test_get.getLeft(0), self.vertex_first)
        self.assertEqual(self.level_test_get.getLeft(-1), self.vertex_last)


if __name__ == "__main__":
    #import sys;sys.argv = ['', 'Test.testName']
    #unittest.main()
    suite = unittest.TestLoader().loadTestsFromTestCase(LevelTest)
    unittest.TextTestRunner(verbosity=2).run(suite)
