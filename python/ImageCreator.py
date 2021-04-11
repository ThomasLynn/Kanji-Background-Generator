import configparser
from PIL import Image
from PIL import ImageFont
from PIL import ImageDraw 

class ImageCreator:
    
    def __init__(self, config_filename):
        self.config = configparser.ConfigParser()
        self.config.read(config_filename)
        
    def create_image(self, data):
        print("creating image from data",data)
        x_size = int(self.config.get('image','x_size', fallback = "300"))
        y_size = int(self.config.get('image','y_size', fallback = "300"))
        scale = y_size * 0.01
        color = self.config.get('image','background_color', fallback = "#101010")
        
        img = Image.new('RGB', (x_size, y_size), color = color)
        draw = ImageDraw.Draw(img)
        
        if bool(self.config.get('kanji','active', fallback = "false")):
            x = float(self.config.get('kanji','x_position', fallback = "0.0")) * scale
            y = float(self.config.get('kanji','y_position', fallback = "0.0")) * scale
            font = self.config.get('kanji','font', fallback = "BIZ-UDGothicB.ttc")
            font_index = int(self.config.get('kanji','font_index', fallback = "0"))
            font_size = float(self.config.get('kanji','font_relative_size', fallback = "10.0")) * scale
            font = ImageFont.truetype(font = font, index = font_index, size = int(font_size))
            color = self.config.get('kanji','text_color', fallback = "#ffffff")
            
            draw.text((int(x), int(y)),data['kanji'],color,font=font)
        
        img.save('images/'+str(data['kanji']+'.png'))