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
        
        if self.config.getboolean('kanji','active', fallback = False):
            x = float(self.config.get('kanji','x_position', fallback = "0.0")) * scale
            y = float(self.config.get('kanji','y_position', fallback = "0.0")) * scale
            font = self.config.get('kanji','font', fallback = "BIZ-UDGothicB.ttc")
            font_index = int(self.config.get('kanji','font_index', fallback = "0"))
            font_size = float(self.config.get('kanji','font_relative_size', fallback = "10.0")) * scale
            font = ImageFont.truetype(font = font, index = font_index, size = int(font_size))
            color = self.config.get('kanji','text_color', fallback = "#ffffff")
            
            draw.text((int(x), int(y)),data['kanji'],color,font=font)
            
        default_x = self.config.get('japanese_default','x_position', fallback = "0.0")
        default_y = self.config.get('japanese_default','y_position', fallback = "0.0")
        default_font = self.config.get('japanese_default','font', fallback = "BIZ-UDGothicB.ttc")
        default_font_index = self.config.get('japanese_default','font_index', fallback = "0")
        default_font_size = self.config.get('japanese_default','font_relative_size', fallback = "10.0")
        default_color = self.config.get('japanese_default','text_color', fallback = "#ffffff")
        
        text_number=0
        while self.config.getboolean('japanese'+str(text_number),'active', fallback = False):
            print("text_number",text_number)
            catagory = 'japanese'+str(text_number)
            x = float(self.config.get(catagory,'x_position', fallback = default_x)) * scale
            y = float(self.config.get(catagory,'y_position', fallback = default_y)) * scale
            font = self.config.get(catagory,'font', fallback = default_font)
            font_index = int(self.config.get(catagory,'font_index', fallback = default_font_index))
            font_size = float(self.config.get(catagory,'font_relative_size', fallback = default_font_size)) * scale
            font = ImageFont.truetype(font = font, index = font_index, size = int(font_size))
            color = self.config.get(catagory,'text_color', fallback = default_color)
            
            try:
                draw.text((int(x), int(y)),data['sentences'][text_number][0],color,font=font)
            except IndexError:
                break
            text_number+=1
            
        default_x = self.config.get('english_default','x_position', fallback = "0.0")
        default_y = self.config.get('english_default','y_position', fallback = "0.0")
        default_font = self.config.get('english_default','font', fallback = "BIZ-UDGothicB.ttc")
        default_font_index = self.config.get('english_default','font_index', fallback = "0")
        default_font_size = self.config.get('english_default','font_relative_size', fallback = "10.0")
        default_color = self.config.get('english_default','text_color', fallback = "#ffffff")
        default_max_width = self.config.get('english_default','max_width', fallback = "100")
            
        text_number=0
        while self.config.getboolean('english'+str(text_number),'active', fallback = False):
            print("text_number english ",text_number)
            catagory = 'english'+str(text_number)
            x = float(self.config.get(catagory,'x_position', fallback = default_x)) * scale
            y = float(self.config.get(catagory,'y_position', fallback = default_y)) * scale
            font = self.config.get(catagory,'font', fallback = default_font)
            font_index = int(self.config.get(catagory,'font_index', fallback = default_font_index))
            font_size = float(self.config.get(catagory,'font_relative_size', fallback = default_font_size)) * scale
            font = ImageFont.truetype(font = font, index = font_index, size = int(font_size))
            color = self.config.get(catagory,'text_color', fallback = default_color)
            max_width = int(self.config.get(catagory,'max_width', fallback = default_max_width))
            
            try:
                text_list = data['sentences'][text_number][1]
                text = text_list[0]
                for i in range(1,len(text_list)):
                    print("adding text",text_list[i])
                    new_text = text + " / "+text_list[i]
                    if len(new_text)<max_width:
                        text = new_text
                    else:
                        break
                print("adding text",text)
                draw.text((int(x), int(y)),text,color,font=font)
            except IndexError:
                print("index error, breaking")
                break
            text_number+=1
        
        img.save('images/'+str(data['kanji']+'.png'))