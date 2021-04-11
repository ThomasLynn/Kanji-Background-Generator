import configparser

class ImageCreator:
    
    def __init__(self, config_filename):
        self.config = configparser.ConfigParser()
        self.config.read(config_filename)
        
    def create_image(self, data):
        print("creating image from data",data)