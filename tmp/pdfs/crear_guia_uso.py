from pathlib import Path
import re, html
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, Preformatted
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_LEFT
pdfmetrics.registerFont(TTFont('Arial', 'C:/Windows/Fonts/arial.ttf'))
pdfmetrics.registerFont(TTFont('ArialBold', 'C:/Windows/Fonts/arialbd.ttf'))
pdfmetrics.registerFontFamily('Arial',normal='Arial',bold='ArialBold',italic='Arial',boldItalic='ArialBold')
s=getSampleStyleSheet()
for n in ['Normal','BodyText','Title','Heading1','Heading2']:
 s[n].fontName='Arial'
s['BodyText'].fontSize=10
s['BodyText'].leading=15
s['BodyText'].spaceAfter=8
s['Title'].fontName='ArialBold'; s['Title'].fontSize=23;s['Title'].leading=28;s['Title'].textColor=colors.HexColor('#283855')
s['Heading2'].fontName='ArialBold';s['Heading2'].fontSize=14;s['Heading2'].leading=19;s['Heading2'].spaceBefore=16;s['Heading2'].spaceAfter=8;s['Heading2'].textColor=colors.HexColor('#283855')
s['Heading2'].keepWithNext=True
cell=ParagraphStyle('cell',parent=s['BodyText'],fontSize=9,leading=12,spaceAfter=0,wordWrap='CJK')
code=ParagraphStyle('code',fontName='Arial',fontSize=9,leading=13,leftIndent=12,backColor=colors.HexColor('#f0f3f8'),borderPadding=9,spaceBefore=8,spaceAfter=12)
def clean(t):
 t=t.replace('→','->').replace('↓','|').replace('—','-').replace('–','-')
 return html.escape(t)
def para(t,style=None):
 texto=clean(t)
 texto=re.sub(r'`([^`]+)`',r'<b>\1</b>',texto)
 return Paragraph(texto,style or s['BodyText'])
lines=Path('GUIA_DE_USO_Y_DEMOSTRACION.md').read_text(encoding='utf-8-sig').splitlines()
story=[];i=0
while i<len(lines):
 l=lines[i].strip()
 if not l:i+=1;continue
 if l.startswith('# '):
  story.append(para(l[2:],s['Title']));story.append(para('Agencia Atenas | Guía de uso y recorrido para la presentación'));story.append(Spacer(1,10));i+=1
 elif l.startswith('### '):story.append(para(l[4:],s['Heading2']));i+=1
 elif l.startswith('## '):story.append(para(l[3:],s['Heading2']));i+=1
 elif l.startswith('```'):
  i+=1;block=[]
  while i<len(lines) and not lines[i].startswith('```'):block.append(lines[i]);i+=1
  story.append(Preformatted('\n'.join(x.replace('↓','|') for x in block),code));i+=1
 elif l.startswith('|'):
  rows=[]
  while i<len(lines) and lines[i].strip().startswith('|'):
   vals=[x.strip() for x in lines[i].strip().strip('|').split('|')]
   if not all(re.fullmatch(r'[:\- ]+',x) for x in vals):rows.append([para(x,cell) for x in vals])
   i+=1
  widths=([130,115,266] if len(rows[0])==3 else [210,301])
  table=Table(rows,colWidths=widths,repeatRows=1,hAlign='LEFT')
  table.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#dde6f4')),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#f4f6fa')]),('VALIGN',(0,0),(-1,-1),'TOP'),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LINEBELOW',(0,0),(-1,0),1,colors.HexColor('#aebed6'))]))
  story.append(table);story.append(Spacer(1,8))
 else:
  story.append(para(l));i+=1
out=Path('output/pdf/GUIA_DE_USO_Y_DEMOSTRACION.pdf')
def footer(c,d):
 c.setStrokeColor(colors.HexColor('#d7dfea'));c.line(42,39,553,39)
 c.setFont('Arial',8);c.setFillColor(colors.HexColor('#59677c'));c.drawString(42,26,'Agencia Atenas - Uso y demostración de Agencia Atenas');c.drawRightString(553,26,str(d.page))
SimpleDocTemplate(str(out),pagesize=(595.28,841.89),rightMargin=42,leftMargin=42,topMargin=40,bottomMargin=55,title='Uso y demostración de Agencia Atenas',author='Equipo Agencia Atenas').build(story,onFirstPage=footer,onLaterPages=footer)
from pypdf import PdfReader
r=PdfReader(out);print('Páginas:',len(r.pages));print('Archivo:',out.resolve())

