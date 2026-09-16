import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of } from 'rxjs';
import { ArticlesComponent } from './articles.component';
import { ArticleService } from 'src/app/services/article.service';
import { Article } from 'src/app/Models/Article';

describe('ArticlesComponent', () => {
  let component: ArticlesComponent;
  let fixture: ComponentFixture<ArticlesComponent>;
  let articleServiceSpy: jasmine.SpyObj<ArticleService>;
  let routerSpy: jasmine.SpyObj<Router>;

  const articles: Article[] = [
    { id: 1, codeArticle: 'ND-CIM-050', designation: 'Ciment CIMAF 50 kg' } as Article,
    { id: 2, codeArticle: 'ND-ALI-310', designation: 'Riz parfumé Indica' } as Article,
    { id: 3, codeArticle: 'ND-ELC-115', designation: 'Ordinateur portable HP' } as Article
  ];

  beforeEach(async () => {
    articleServiceSpy = jasmine.createSpyObj('ArticleService', ['getAllArticles']);
    articleServiceSpy.getAllArticles.and.returnValue(of(articles));
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [ArticlesComponent],
      providers: [
        { provide: ArticleService, useValue: articleServiceSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ArticlesComponent);
    component = fixture.componentInstance;
  });

  it('se crée et charge les articles', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
    expect(component.articlesList.length).toBe(3);
    expect(component.articlesFiltres.length).toBe(3);
  });

  it('filtre les articles par désignation', () => {
    fixture.detectChanges();
    component.termeRecherche = 'riz';
    component.appliquerFiltre();
    expect(component.articlesFiltres.length).toBe(1);
    expect(component.articlesFiltres[0].codeArticle).toBe('ND-ALI-310');
  });

  it('filtre les articles par code', () => {
    fixture.detectChanges();
    component.termeRecherche = 'nd-elc';
    component.appliquerFiltre();
    expect(component.articlesFiltres.length).toBe(1);
    expect(component.articlesFiltres[0].id).toBe(3);
  });

  it('terme vide renvoie tous les articles', () => {
    fixture.detectChanges();
    component.termeRecherche = '   ';
    component.appliquerFiltre();
    expect(component.articlesFiltres.length).toBe(3);
  });

  it('nouvelArticle navigue vers la création', () => {
    fixture.detectChanges();
    component.nouvelArticle();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['newarticles']);
  });
});
