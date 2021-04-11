import configparser
from PIL import Image
from PIL import ImageFont
from PIL import ImageDraw 
from pathlib import Path
from StringSimilarity import *

class ImageCreator:
    
    def __init__(self, config_filename):
        self.config = configparser.ConfigParser()
        self.config.read(config_filename)
        
    def create_image(self, data, output_folder):
        print("creating image from data",data)
        
        # create the output folder if it doesn't exist
        Path(output_folder).mkdir(parents=True, exist_ok=True)
        
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
            align = self.config.get('kanji','align', fallback = "left")
            offset = 0
            text_width = font.getsize(data['kanji'])[0]
            if align == "center":
                offset = text_width/2
            if align == "right":
                offset = text_width
            
            draw.text((int(x - offset), int(y)),data['kanji'],color,font=font, align = align)
        
        if self.config.getboolean('meaning','active', fallback = False):
            x = float(self.config.get('meaning','x_position', fallback = "0.0")) * scale
            y = float(self.config.get('meaning','y_position', fallback = "0.0")) * scale
            font = self.config.get('meaning','font', fallback = "arial.ttf")
            font_index = int(self.config.get('meaning','font_index', fallback = "0"))
            font_size = float(self.config.get('meaning','font_relative_size', fallback = "10.0")) * scale
            font = ImageFont.truetype(font = font, index = font_index, size = int(font_size))
            color = self.config.get('meaning','text_color', fallback = "#ffffff")
            align = self.config.get('meaning','align', fallback = "left")
            offset = 0
            text_width = font.getsize(data['meaning'])[0]
            if align == "center":
                offset = text_width/2
            if align == "right":
                offset = text_width
            
            draw.text((int(x - offset), int(y)),data['meaning'],color,font=font, align = align)
            
        default_x = self.config.get('japanese_default','x_position', fallback = "0.0")
        default_y = self.config.get('japanese_default','y_position', fallback = "0.0")
        default_font = self.config.get('japanese_default','font', fallback = "meiryo.ttc")
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
            align = self.config.get(catagory,'align', fallback = "left")
            offset = 0
            text_width = font.getsize(data['sentences'][text_number][0])[0]
            if align == "center":
                offset = text_width/2
            if align == "right":
                offset = text_width
            
            try:
                draw.text((int(x - offset), int(y)),data['sentences'][text_number][0],color,font=font, align = align)
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
                #print("text_list",text_list)
                text_list = sort_similar(text_list)
                #print("text_list2",text_list)
                text = text_list[0]
                for i in range(1,len(text_list)):
                    print("adding text",text_list[i])
                    new_text = text + " / "+text_list[i]
                    if len(new_text)<max_width:
                        text = new_text
                    else:
                        break
                #print("adding text",text)
                align = self.config.get(catagory,'align', fallback = "left")
                offset = 0
                text_width = font.getsize(text)[0]
                if align == "center":
                    offset = text_width/2
                if align == "right":
                    offset = text_width
                draw.text((int(x - offset), int(y)),text,color,font=font)
            except IndexError:
                print("index error, breaking")
                break
            text_number+=1
        
        img.save(output_folder+'/'+str(data['kanji']+'.png'))