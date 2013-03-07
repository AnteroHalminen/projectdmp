from django.views.generic import TemplateView

class FrontPage(TemplateView):
    template_name = "frontpage.html"

    def get_context_data(self, **kwargs):
        return {'name': 'frontpage'}
